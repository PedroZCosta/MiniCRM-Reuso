using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class MotivoPerdaController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public MotivoPerdaController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/MotivoPerda
        [HttpGet]
        public async Task<ActionResult<IEnumerable<MotivoPerda>>> GetMotivoPerda()
        {
            return await _context.MotivoPerda.ToListAsync();
        }

        // GET: api/MotivoPerda/5
        [HttpGet("{id}")]
        public async Task<ActionResult<MotivoPerda>> GetMotivoPerda(sbyte id)
        {
            var motivoPerda = await _context.MotivoPerda.FindAsync(id);

            if (motivoPerda == null)
            {
                return NotFound();
            }

            return motivoPerda;
        }

        // PUT: api/MotivoPerda/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutMotivoPerda(sbyte id, MotivoPerda motivoPerda)
        {
            if (id != motivoPerda.IdMotivo)
            {
                return BadRequest();
            }

            _context.Entry(motivoPerda).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!MotivoPerdaExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/MotivoPerda
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<MotivoPerda>> PostMotivoPerda(MotivoPerda motivoPerda)
        {
            _context.MotivoPerda.Add(motivoPerda);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetMotivoPerda", new { id = motivoPerda.IdMotivo }, motivoPerda);
        }

        // DELETE: api/MotivoPerda/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteMotivoPerda(sbyte id)
        {
            var motivoPerda = await _context.MotivoPerda.FindAsync(id);
            if (motivoPerda == null)
            {
                return NotFound();
            }

            _context.MotivoPerda.Remove(motivoPerda);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool MotivoPerdaExists(sbyte id)
        {
            return _context.MotivoPerda.Any(e => e.IdMotivo == id);
        }
    }
}
