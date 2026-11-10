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
    public class MotivoPerdasController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public MotivoPerdasController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/MotivoPerdas
        [HttpGet]
        public async Task<ActionResult<IEnumerable<MotivoPerda>>> GetMotivoPerdas()
        {
            return await _context.MotivoPerdas.ToListAsync();
        }

        // GET: api/MotivoPerdas/5
        [HttpGet("{id}")]
        public async Task<ActionResult<MotivoPerda>> GetMotivoPerda(sbyte id)
        {
            var motivoPerda = await _context.MotivoPerdas.FindAsync(id);

            if (motivoPerda == null)
            {
                return NotFound();
            }

            return motivoPerda;
        }

        // PUT: api/MotivoPerdas/5
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

        // POST: api/MotivoPerdas
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<MotivoPerda>> PostMotivoPerda(MotivoPerda motivoPerda)
        {
            _context.MotivoPerdas.Add(motivoPerda);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetMotivoPerda", new { id = motivoPerda.IdMotivo }, motivoPerda);
        }

        // DELETE: api/MotivoPerdas/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteMotivoPerda(sbyte id)
        {
            var motivoPerda = await _context.MotivoPerdas.FindAsync(id);
            if (motivoPerda == null)
            {
                return NotFound();
            }

            _context.MotivoPerdas.Remove(motivoPerda);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool MotivoPerdaExists(sbyte id)
        {
            return _context.MotivoPerdas.Any(e => e.IdMotivo == id);
        }
    }
}
